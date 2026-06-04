## ADDED Requirements

### Requirement: Public privacy and store metadata describe data handling
The project SHALL maintain public privacy, release, Google Play, and F-Droid
documentation that reflects Verbally's local-first BYOK behavior.

#### Scenario: Privacy policy is published
- **WHEN** a store reviewer or user opens the repository privacy policy
- **THEN** it explains local storage, temporary audio, AI provider transmission,
  AccessibilityService API use, and local history retention

#### Scenario: F-Droid metadata is reviewed
- **WHEN** F-Droid metadata is prepared
- **THEN** it identifies Apache-2.0 licensing
- **AND** it declares the `NonFreeNet` anti-feature for user-selected proprietary AI
  network services

#### Scenario: Google Play data safety is prepared
- **WHEN** Play Console Data safety is filled
- **THEN** the maintainer can use the repository draft to align declarations with the
  privacy policy and app behavior
