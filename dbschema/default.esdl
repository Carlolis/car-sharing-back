module default {
  type TripGel {
    required name: str;
    required date: cal::local_date;
    required distance: int16;
    required multi gelDrivers: PersonGel;
  }

  type PersonGel {
    required name: str;
    multi drive_in := .<gelDrivers[is TripGel];
  }

  type WriterGel {
    required name: str;
    multi chats : ChatSessionGel{
             constraint exclusive;
             on target delete allow;
    }
  }

  type ChatSessionGel {
      required title: str;
      multi messages: MessageGel{
               constraint exclusive;
      }
  }

  type MessageGel {
        required question: str;
        required answer: str;
  }

}
using extension ai;