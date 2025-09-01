CREATE MIGRATION m1iygl752msdj2hb5xews47cbmjikm2h7m6nijvfqr6dezk5nhy4nq
    ONTO m15nj4gzcusf2ar2gqj2j3pb4zeg6fjnfw33fuwpswdn4nrkuvxisa
{
  ALTER TYPE default::InvoiceGel {
      DROP PROPERTY isReimbursement;
  };
};
