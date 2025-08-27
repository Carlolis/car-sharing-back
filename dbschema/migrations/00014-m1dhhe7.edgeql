CREATE MIGRATION m1dhhe75z5noskkmajojz5unyzocev4iryopbb2nsrv5lmytlowrkq
    ONTO m1pui5e72tvxqzezrtlfk323ftc3rdzpaejxjjnkvvlxueawhxxq6a
{
  ALTER TYPE default::TripGel {
      ALTER PROPERTY distance {
          RESET OPTIONALITY;
      };
  };
};
