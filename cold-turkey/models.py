import endpoints
from google.appengine.ext import ndb
from scheduleslist_message import SchedulesResponseMessage

def get_endpoints_current_user(raise_unauthorized=True):
    current_user = endpoints.get_current_user()
    if raise_unauthorized and current_user is None:
        raise endpoints.UnauthorizedException("Invalid token.")
    return current_user

class Schedules(ndb.Model):
    schedule_id = ndb.TextProperty(required=True)
    schedule_packages  = ndb.TextProperty(required=True)
    schedule_start_datetime = ndb.TextProperty(required=True)
    schedule_end_datetime = ndb.TextProperty(required=True)
    
    def to_message(self):
        return SchedulesResponseMessage(schedule_id=self.schedule_id,
                                        schedule_packages=self.schedule_packages,
                                        schedule_start_datetime=self.schedule_start_datetime,
                                        schedule_end_datetime=self.schedule_end_datetime)

    @classmethod
    def getSchedule(cls):
        return cls.query()
