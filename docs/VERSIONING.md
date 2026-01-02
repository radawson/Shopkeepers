# Versioning Scheme

Shopkeepers uses [Semantic Versioning](https://semver.org/) (SemVer) for version numbers.

## Version Format

The version format is: **`Major.Minor.Patch[-pre.id][+metadata]`**

### Version Components

- **Major**: Incremented for backwards incompatible changes (API breaking changes, behavior changes that break existing functionality)
- **Minor**: Incremented for backwards compatible feature additions and enhancements
- **Patch**: Incremented for backwards compatible bug fixes

### Pre-Releases

Pre-release versions can be denoted using identifiers (e.g., `3.0.0-alpha`, `3.0.0-beta.3`, `3.0.0-rc.1`).

During pre-releases, anything may change at any time. The public API should not be considered stable.

### Build Metadata

Additional build metadata can be appended using `+` (e.g., build numbers, build dates, commit hashes):

- `3.0.0+20240101`
- `3.0.0+build.123`

Build metadata does not affect version precedence.

## Version Precedence

Version precedence follows the standard Semantic Versioning rules:

1. Major, Minor, and Patch are compared numerically
2. Pre-release versions have lower precedence than the normal version
3. Build metadata is ignored when determining precedence

Examples:

- `3.0.0` < `3.0.1` < `3.1.0` < `4.0.0`
- `3.0.0-alpha` < `3.0.0-beta` < `3.0.0-rc.1` < `3.0.0`

## Version Assignment

- Versions are typically assigned for (pre-)releases
- Each new release should increment at least the Patch component
- Individual commits and development builds might not get individual versions assigned (they might only differ in their build metadata, e.g., build number, date, etc.)

## Breaking Changes

When updating between major versions (e.g., 2.x.x to 3.x.x), expect:

- API changes that may require code updates
- Configuration format changes (auto-migration is provided when possible)
- Behavior changes that may affect existing setups

Always review the changelog when upgrading across major versions.
