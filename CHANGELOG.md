# Changelog

## [Unreleased] - 2025-10-20

### Fixed
- Updated AIToolMapper to include makerId parameter when querying tools by name for better precision
- Modified AIToolAgentServiceHandler to properly handle tool creation with makerId parameter
- Ensured proper tool selection by both makerId and name in service handler
- Corrected tool creation in AIToolAgentServiceHandler to use req.maker.id instead of m.id
- Set proper agent status and ID when creating new tools
- Updated AIToolServiceHandler to use AIPortToolPermissionMakerSummary in grantToolPermission response
- Modified grantToolPermission method to return tool permission maker summaries from both regular and virtual permissions

### Added
- Generated entity classes from SQL schema files in sql/ directory
- Added AIPortAccount entity based on account.sql schema
- Added AIPortAITool entity based on aitool.sql schema
- Added AIPortToolAgent entity based on aitool.sql schema
- Added AIPortToolMaker entity based on aitool.sql schema
- Added AIPortAccessKey entity based on account.sql schema
- Added AIPortUser entity based on account.sql schema
- Added AIPortTeam entity based on account.sql schema
- Added AIPortTeamMember entity based on account.sql schema
- Added AIPortToolPermission entity based on aitool.sql schema
- Included public fields and primitive types in generated entities

### Removed
- Removed standalone entity files as they are now integrated with existing entity structure

## [Unreleased] - 2025-10-14

### Added
- Team-tool maker functionality with new entity AIPortTeamToolMaker and mapper TeamToolMakerMapper
- New endpoints for managing tool makers by team: team/modify and team/query in AIToolMakerServiceHandler
- Integration between team and tool maker systems
- Additional fields to AIPortToolMaker and AIPortToolPermission entities
- Method to query tool makers by team ID in ToolMakerMapper

### Changed
- Verified that user_id in aitool.tool_maker table is already properly synchronized with corresponding tool_agent's user_id when type is 1000
- No update operation needed as all records of type 1000 already have matching user_id values
- Enhanced ToolMakerMapper to include user_id in SELECT_FIELDS and adjust related queries
- Modified service methods to properly handle user_id for tool makers
- Updated queryToolMakers method to include team-based and user-based lookups
- Added response success in createToolMaker method
- Updated query parameters for tool maker filtering
- Consolidated tool maker modification endpoints into a single 'modify' method with comprehensive functionality
- Improved security checks for tool maker modifications to ensure proper user ownership
- Updated ToolAgentMapper with improved method signatures
- Added comprehensive 'modify' method for tool agents allowing name, tags, and status updates
- Standardized parameter naming across various service handlers

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
- New entity class AIPortToolPermission for tool permission management
- New entity class AIPortToolPermissionMakerSummary for tool permission maker summary
- New entity class AIPortUserAccount for user account management
- New error handling class AccountServiceErrors for account service error management

### Changed
- Updated AIPortVirtualToolPermission entity with additional fields and methods
- Enhanced ToolPermissionMapper with new permission-related operations
- Modified service handlers: AIToolAgentServiceHandler, AIToolMakerServiceHandler, AIToolServiceHandler with improved functionality
- Updated ToolMakerMapper with additional methods for tool maker management
- Enhanced AIToolDiscoveryServiceHandler and AIToolServiceHandler with improved discovery and service capabilities
- Updated AccountServiceHandler with additional account management features
- Further enhanced AccountServiceHandler with improved account management capabilities
- Updated pom.xml with dependency and configuration changes
- Enhanced ServiceRequestAuthenticationHandler with improved authentication handling
- Refactored service handlers from admin package to service package: AIToolAgentServiceHandler, AIToolDiscoveryServiceHandler, AIToolMakerServiceHandler, AIToolServiceHandler, AccessKeyServiceHandler, AccountServiceHandler, AuthenticationServiceHandler, ServiceRequestAuthenticationHandler
- Updated AIPortAccount, AIPortTeam, AIPortTeamMember, AIPortUserAccount, and AIPortToolMaker entities with additional fields and methods
- Enhanced TeamMapper and ToolMakerMapper with additional methods and capabilities
- Updated AIPortTeam and AIPortUser entities with additional fields and methods
- Enhanced AccountMapper and TeamMapper with additional methods and capabilities
- Improved AccountServiceHandler with enhanced account and team management features
- Further enhanced TeamMapper with additional team-related operations
- Updated AccountServiceHandler with additional account management capabilities
- Enhanced AIPortApiKeyGeneratorTest with additional test cases
- Updated AIPortToolMaker and AIPortToolMakerTeam entities with additional fields and methods
- Enhanced ToolMakerMapper with additional methods for tool maker management

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