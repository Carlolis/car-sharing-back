CREATE MIGRATION m1bsch5yfw6fo7esdttmuqzeg4cgm2n3bg7kirwg6zdlbjgfuc3xkq
    ONTO m1qpllogvdeo73lsp6qtn46qf5hgofrz6p3tkczo4csq6j3tk3jdvq
{
  ALTER TYPE default::InvoiceGel {
      CREATE REQUIRED PROPERTY kind: std::str {
          SET REQUIRED USING (<std::str>{'tes'});
      };
  };
};
