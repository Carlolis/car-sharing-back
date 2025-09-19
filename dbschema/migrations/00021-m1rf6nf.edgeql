CREATE MIGRATION m1rf6nfgck5vnuxszklln6gfjevllzpbq5o2evbw3hntwqrqckz4fa
    ONTO m1zbz4dezjx7s5dmbs7vufsig35bsyzuidpvr3o6egjdvjm45grxla
{
  ALTER TYPE default::MaintenanceGel {
      ALTER PROPERTY completedMileage {
          SET TYPE std::int64;
      };
      ALTER PROPERTY dueMileage {
          SET TYPE std::int64;
      };
  };
};
