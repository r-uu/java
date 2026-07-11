# bom - bill of materials
Maven module for bill of materials that are commonly used by other r-uu maven modules. This module defines neither a maven parent nor maven children. It provides common configuration of maven repositories, dependencies, properties, profiles and others.

✅ bom may contain

- dependency management for external dependencies including versions and scopes
- plugin management and plugin configurations
- properties (java-version, encoding, compiler-flags, ...)
- repository-definitions
- build-configurations (annotation processing, etc.)
- common profiles

❌ bom does not contain

|                                    |                                                  |
|------------------------------------|--------------------------------------------------|
| \<modules> section                 | bom does not know about depending maven modules! |
| maven module-specific dependencies | only commonly used external dependencies         |
| deployment-configuration           | that is module-specific                          |
| concrete dependencies              | only \<dependencyManagement>/\<pluginManagement> |
