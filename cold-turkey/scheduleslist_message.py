from protorpc import messages
    
class SchedulesResponseMessage(messages.Message):
    schedule_id = messages.StringField(1)
    schedule_packages = messages.StringField(2)
    schedule_start_datetime = messages.StringField(3)
    schedule_end_datetime = messages.StringField(4)

class SchedulesListResponse(messages.Message):
    items = messages.MessageField(SchedulesResponseMessage, 1, repeated=True)
