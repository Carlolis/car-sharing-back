CREATE MIGRATION m1pui5e72tvxqzezrtlfk323ftc3rdzpaejxjjnkvvlxueawhxxq6a
    ONTO m1bsch5yfw6fo7esdttmuqzeg4cgm2n3bg7kirwg6zdlbjgfuc3xkq
{
  ALTER TYPE default::InvoiceGel {
      CREATE PROPERTY mileage: std::int16;
  };
};
