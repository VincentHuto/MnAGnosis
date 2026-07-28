# Stage 11 — Claim: No Consequence Precedes Me

## Purpose

Deliver the Continuance-focused portion of the main duel. Yaldabaoth separates
declared attacks from their consequences and claims exemption from honest
sequence.

## Combat Flow

This Claim enters near 40–10% boss health.

Eligible events include:

- A visible tail path whose damage resolves later.
- A roar whose knockback is delayed.
- A safe sector that returns as a recorded hazard.
- A body impact heard before or after its visible strike.

Every event has a stable identifier, declaration, path, consequence,
expiration, and conclusion state. The player captures one declared sequence
with Continuance and forces it to conclude in its advertised order.

An honestly concluded event:

- Resolves its retained consequence exactly once.
- Cannot be redirected into an unrelated target.
- Stuns Yaldabaoth.
- Exposes the designated contradiction segments.

## Readability

Declaration and consequence use paired symbols and matching subtitles.
Delayed events remain visible in a restrained timeline indicator or
world-space trace. A player should never need to remember more than two
unresolved high-attention events in solo play.

## Failure and Persistence

Expired captures return to the normal attack ledger. Invalid or orphaned event
records are cancelled without damage during recovery. No delayed event may
survive encounter completion or arena restoration.

## Party Scaling

Groups may receive additional low-attention recorded events, but only one
honest conclusion is required per damage window. Multiple captures cannot
resolve the same consequence more than once.

## Verification

- Declaration, capture, consequence, and conclusion remain one-to-one.
- Save/reload does not duplicate or silently drop an active consequence.
- Honest conclusion exposes the correct segments and applies one stun.
- Invalid capture feedback consumes no Continuance action.
- All ledgers are empty after wipe, victory, and cleanup.

## Handoff

Supplies the delayed-event ledger to
[Stage 12](12-claims-and-crown-transition.md).
