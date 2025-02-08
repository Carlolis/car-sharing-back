module default {
  type TripEdge {
    required name: str;
    required date: cal::local_date;
    required distance: int16;
    required multi edgeDrivers: PersonEdge;

  }

  type PersonEdge {
    required name: str;
    multi drive_in := .<edgeDrivers[is TripEdge];
  }



}
using extension ai;