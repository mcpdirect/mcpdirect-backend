# Changelog

## [Unreleased] - 2025-10-08

### Added
- New entity classes for team management: AIPortTeam, AIPortTeamMember, AIPortToolMakerTeam
- Virtual tool entities: AIPortVirtualTool and AIPortVirtualToolPermission
- TeamMapper interface for team-related database operations
- Updated service handlers: AIToolAgentServiceHandler, AIToolDiscoveryServiceHandler, AIToolMakerServiceHandler, AIToolServiceHandler, AccountServiceHandler
- Updated mapper interfaces: AIToolMapper, ToolAgentMapper, ToolMakerMapper, VirtualToolMapper, AccountMapper
- Test class: AIPortApiKeyGeneratorTest
- Modifications to account and aitool SQL schema files
- Configuration updates in .qwen/settings.json, QWEN.md, and pom.xml

## [1.2.5] - 2025-10-05

### Added
- TeamMapper interface with select, insert, and update methods for team and team_member tables

## [1.2.4] - 2025-10-05

### Added
- AIPortTeam and AIPortTeamMember entity classes for team management

## [1.2.3] - 2025-09-11

### Changed
- Updated hstp-service-engine dependency from version 1.4.1 to 1.4.2

## [1.2.2] - 2025-09-11

### Changed
- Updated project version to 1.1.2-SNAPSHOT
- Updated hstp-service-engine dependency from version 1.4.0 to 1.4.1

## [1.2.1] - 2025-09-10

### Added
- Database schema SQL files for account and aitool modules
- App version table schema with platform and architecture support

### Changed
- Updated project version to 1.1.1-SNAPSHOT
- Refactored AccessKeyServiceHandler to use KeyValueCacheFactory directly
- Removed unused instance variable in AccessKeyServiceHandler

## [1.2.0] - 2025-09-10

### Added
- App version endpoint for client version checking
- Response structure for app version information including force upgrade flag

### Changed
- Updated hstp-service-engine dependency from version 1.3.0 to 1.4.0
- Removed unused lookup/tools/provider service endpoint code

## [1.1.0] - 2025-08-30

### Added
- Key seed generation for account security
- Device ID tracking for tool agents
- Account key seed field to authentication responses

### Changed
- Project renamed from AIPort Admin to MCPdirect backend
- Version bumped to 1.1.0-SNAPSHOT
- Authentication service to include key seed in responses
- Account entities to include key seed field
- Tool agent entities to include device ID field
- Database mappers updated to handle new fields
- Access key generator improvements