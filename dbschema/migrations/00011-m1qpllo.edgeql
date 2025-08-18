CREATE MIGRATION m1qpllogvdeo73lsp6qtn46qf5hgofrz6p3tkczo4csq6j3tk3jdvq
    ONTO m1niyi3gnvgsgjc7klp6iukyedxa7i623dgdprhwntm2y44kqw2u4q
{
  ALTER TYPE default::TripGel {
      CREATE PROPERTY comments: std::str;
  };
};
