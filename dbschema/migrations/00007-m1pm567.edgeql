CREATE MIGRATION m1pm567a5ugsgzv2chj22p4tht2mmtupjtr4kpahmc45tx7jfj24xa
    ONTO m1mlp6kxa4o3zmz6p3ubgr5mcpmmt5wcot5trj3n6wpdtktmrx22bq
{
  ALTER TYPE default::ChatSessionGel {
      ALTER LINK messages {
          ON SOURCE DELETE DELETE TARGET;
      };
  };
};
