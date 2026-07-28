# Yaldabaoth Celestial Formation Design

## Goal

Make the Counterfeit Sun and Counterfeit Moon permanent members of Yaldabaoth's encounter composition. They spawn with him, remain locked to his right and left sides as he turns or moves, bob in opposite phases, and return 20 seconds after being destroyed.

## Formation

The formation is defined in Yaldabaoth's local coordinate space:

- The Counterfeit Sun remains six blocks to Yaldabaoth's right.
- The Counterfeit Moon remains six blocks to Yaldabaoth's left.
- Both have a base vertical offset of five blocks, placing them near the raised head rather than at ground level.
- Both share Yaldabaoth's facing so the formation rotates as one composition.
- There is no continuous orbit and the celestials never exchange sides.

Each celestial adds a sinusoidal vertical offset with a four-second period and 0.75-block amplitude. The Moon is exactly half a cycle out of phase with the Sun: when one reaches the top of its bob, the other reaches the bottom.

The target formation position is recalculated every tick from Yaldabaoth's current position and yaw. This carries the celestials with both turns and translational movement, including the slithering state.

## Ownership

Yaldabaoth owns at most one Sun and one Moon:

- Yaldabaoth stores the UUID assigned to each owned celestial.
- Each owned celestial stores Yaldabaoth's UUID.
- Ownership survives save and load.
- Existing ownerless Sun and Moon entities remain valid independent summons. They retain their current stationary behavior and never attach themselves to a nearby Yaldabaoth.

On the first server tick after a new Yaldabaoth is created, missing initial companions spawn immediately in their assigned formation positions. Save/load recovery resolves the stored UUIDs and searches for already-owned companions before creating replacements, preventing duplicates caused by entity load order.

## Destruction and Respawn

The Sun and Moon have independent lifecycles:

- When an owned celestial is killed, it notifies its owner and clears that role's active UUID.
- The corresponding respawn timer starts at exactly 400 ticks.
- The surviving celestial remains present and continues following the formation.
- The timer advances only while Yaldabaoth is loaded and alive.
- At zero, Yaldabaoth creates a new celestial of the missing role directly at its current formation position and records the new UUID.

If a companion becomes missing without a normal death notification, Yaldabaoth first attempts UUID resolution and then scans a 16-block radius for a celestial of the correct role carrying his owner UUID. If it is genuinely absent, the same 400-tick delayed return begins. This preserves the "always accompanied" rule without creating duplicates during normal chunk loading.

If Yaldabaoth dies or is permanently removed, both owned celestials are removed and their respawn timers stop. Ownerless celestials are unaffected.

## Runtime Responsibilities

A small pure formation component will calculate:

- the right or left world-space offset from Yaldabaoth's yaw;
- the shared height plus the role's opposite-phase bob;
- countdown transitions for present, waiting, and ready-to-respawn states.

`YaldabaothEntity` owns spawning, stored companion UUIDs, respawn countdowns, duplicate recovery, and death cleanup.

`AbstractCelestialEntity` owns the synchronized optional owner UUID, persistent owner data, owner resolution, formation following, and notification when an owned celestial dies. The concrete Sun and Moon classes identify their fixed formation role.

Server state is authoritative. The entity types already use a one-tick update interval, so their normal entity synchronization supplies client interpolation while preserving server-side positions and hit detection.

## Compatibility

This change preserves:

- the current Sun, Moon, and Yaldabaoth entity registrations;
- all three GeckoLib models, textures, and animation identifiers;
- celestial allegiance and combat animation state;
- independent command or spawn-egg creation of ownerless celestials;
- Yaldabaoth's current idle, movement, and combat animations.

It does not add celestial attacks, change health or attributes, alter Yaldabaoth's movement logic, or make ownerless celestials search for an owner.

## Verification

Unit tests will verify formation math at representative cardinal yaws, exact right/left role separation, opposite bob phases, amplitude bounds, period closure, and the 400-tick countdown boundary.

GameTests will verify:

- a newly spawned Yaldabaoth creates exactly one owned Sun and one owned Moon;
- repeated ticks do not create duplicates;
- turning Yaldabaoth preserves Sun-right and Moon-left placement;
- moving Yaldabaoth carries both companions;
- killing one companion starts only its respawn delay;
- the destroyed role remains absent through tick 399 and returns at tick 400;
- the surviving role is not replaced;
- ownership and timers survive NBT;
- removing Yaldabaoth removes both owned companions;
- ownerless celestial entities remain independent.

Final visual acceptance requires observing the trio in a development client: the Sun must remain on Yaldabaoth's apparent right, the Moon on his apparent left, their bobbing must be smooth and opposite, and neither companion may visibly snap or trail excessively during ordinary turning and movement.
