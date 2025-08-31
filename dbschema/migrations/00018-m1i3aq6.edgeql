CREATE MIGRATION m15nj4gzcusf2ar2gqj2j3pb4zeg6fjnfw33fuwpswdn4nrkuvxisa
    ONTO m1ltjqodhno3vxntckm5t2y5x2y5okfhip5kv6eijbpeldllgxnfxa
{
  ALTER TYPE default::InvoiceGel {
      ALTER PROPERTY amount {
          SET TYPE std::decimal USING (<std::decimal>.amount);
      };
  };
};
