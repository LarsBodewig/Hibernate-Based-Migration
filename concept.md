freeze goal: save version of entity classes with version prefix
thaw goal: add saved version of entity classes to compile sources
HibernateMigration can be used to migrate data between version by using flyway 

HibernateMigration has to use frozen versions in order to be applicable later on
freeze goal uses maven version
therefore each migration script needs a version

how about making a single goal that creates a flyway object, calculates the newest version, freezes the current entity classes and executes the migration,
however then the migration needs to be prefixed with version after compilation :/


almost there
need a new goal that invokes flyway info after compile and can be executed in a parallel lifecycle with the execute annotation. it has to write its results into a file or a maven template expression so the current mojo can read the flyway info back before and run before the actual compile phase!
