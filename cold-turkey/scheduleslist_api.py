import endpoints
from protorpc import remote
from protorpc import message_types
from models import Schedules
from scheduleslist_message import SchedulesListResponse

CLIENT_ID = "YOUR-CLIENT-ID"

@endpoints.api(name="getSchedules", version="v1",
               description= "The getSchedules API gets schedules from the database.",
               allowed_client_ids=[CLIENT_ID, endpoints.API_EXPLORER_CLIENT_ID])

class GetSchedulesApi(remote.Service):
    @endpoints.method(message_types.VoidMessage, SchedulesListResponse,
                      path="schedules", http_method="POST",
                      name="schedules.list")
    def schedules_list(self, unused_request):
        query = Schedules.getSchedule()
        items = [entity.to_message() for entity in query.fetch()]
        return SchedulesListResponse(items=items)
    
application = endpoints.api_server([GetSchedulesApi],
                                   restricted=False)
