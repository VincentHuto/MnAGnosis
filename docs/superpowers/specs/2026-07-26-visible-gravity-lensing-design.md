# Visible Gravity Lensing Design

## Goal

Make every rendered Gravity Convergence black hole visibly bend the level
behind it. The existing square lattice particles remain active as secondary
field-volume feedback.

## Diagnosis

The event horizon is rendered 1.5 blocks above the `GravityFieldEntity`
origin, but `GravityLensController` currently projects and raycasts against
the origin. The post-process can therefore be centered below the sphere or
culled by terrain at the entity's feet. Its current single scene sample also
moves by too little to read clearly during play.

## Visual Contract

- Project, frustum-test, and raycast against the same elevated visual center
  used by the black sphere.
- Bend actual framebuffer samples in a bounded halo around that center.
- Stretch scenery radially toward the event horizon.
- Near the photon ring, blend a second sample from the opposite side of the
  lens to create a recognizable mirrored Einstein band.
- Keep the effect readable for both attraction and repulsion; polarity changes
  the direction of the outer breathing distortion, not whether lensing exists.
- Affect at most 4.5 event-horizon radii and leave HUD rendering untouched.
- Do not alter or replace `GravityFieldEntity` lattice particles.

## Safety and Performance

The post chain retains its existing three-lens cap. Each affected pixel uses
one primary scene sample and, only inside a narrow horizon band, one mirrored
sample. Pixels outside all bounded halos follow the normal single-sample path.

