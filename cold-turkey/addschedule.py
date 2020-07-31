import webapp2
from models import Schedules

class AddScheduleHandler(webapp2.RequestHandler):
    def get(self):
        schedule = Schedules(schedule_id=self.request.get("schedule_id"),
                             schedule_packages=self.request.get("schedule_packages"),
                             schedule_start_datetime=self.request.get("schedule_start_datetime"),
                             schedule_end_datetime=self.request.get("schedule_end_datetime"))
        schedule.put()
        
        self.response.write("Schedule added")

application = webapp2.WSGIApplication([
    ("/", AddScheduleHandler)
], debug=True)
