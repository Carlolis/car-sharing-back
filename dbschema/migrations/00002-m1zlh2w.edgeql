CREATE MIGRATION m1zlh2wskymoitnqj6j2gwsvucdqqyahoyk6nfi6oyytvzwbdoaoxa
    ONTO m1mdvi3sivkj34xaqpuuzycbjsj5vsqh32cqbf6ul3ob3ccgdksgea
{
  CREATE EXTENSION pgvector VERSION '0.5';
  CREATE EXTENSION ai VERSION '1.0';
  ALTER TYPE default::Person RENAME TO default::PersonEdge;
  ALTER TYPE default::Travel RENAME TO default::TripEdge;
  ALTER TYPE default::TripEdge {
      ALTER LINK driver {
          RENAME TO edgeDrivers;
      };
  };
  ALTER TYPE default::PersonEdge {
      CREATE MULTI LINK drive_in := (.<edgeDrivers[IS default::TripEdge]);
  };
};
