CREATE MIGRATION m1zbz4dezjx7s5dmbs7vufsig35bsyzuidpvr3o6egjdvjm45grxla
    ONTO m1iygl752msdj2hb5xews47cbmjikm2h7m6nijvfqr6dezk5nhy4nq
{
  CREATE TYPE default::MaintenanceGel {
      CREATE LINK invoice: default::InvoiceGel;
      CREATE PROPERTY completedDate: std::cal::local_date;
      CREATE PROPERTY completedMileage: std::int16;
      CREATE PROPERTY description: std::str;
      CREATE PROPERTY dueDate: std::cal::local_date;
      CREATE PROPERTY dueMileage: std::int16;
      CREATE REQUIRED PROPERTY isCompleted: std::bool;
      CREATE REQUIRED PROPERTY type: std::str;
  };
};
