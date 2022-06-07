from urllib.parse import urlparse
import json

lang_codes = json.loads(open("language-and-locale-codes.json").read())


class Event:
    def __init__(self, event_type: str = None, event_id: int = None, timestamp: str = None, username: str = None,
                 user_type: str = None, language: str = None, title: str = None):
        self.event_type = event_type
        self.event_id = event_id
        self.timestamp = timestamp
        self.username = username
        self.user_type = user_type
        self.language = language
        self.title = title

    @staticmethod
    def get_matching_event(event_data):
        return CreatePageEvent(event_data) if "create" in event_data["$schema"] else EditPageEvent(event_data)

    @staticmethod
    def get_language(url: str) -> str:
        for lang in lang_codes:
            if lang["code"] + "." in url or "." + lang["code"] in url:
                return lang["desc"]
        return "English"

    def toJSON(self):
        return json.dumps(self.__dict__, sort_keys=True, indent=4)


class EditPageEvent(Event):
    def __init__(self, event_data):
        self.type = "edit"
        self.event_id = event_data['id']
        self.timestamp = event_data['meta']['dt']
        self.username = event_data['user']
        self.user_type = "bot" if event_data['bot'] else "user"
        self.language = self.get_language(urlparse(event_data["meta"]["uri"]).netloc)
        self.title = event_data['title']
        self.is_revert = False


class CreatePageEvent(Event):
    def __init__(self, event_data):
        self.type = "new"
        self.event_id = event_data['rev_id']
        self.timestamp = event_data['meta']['dt']
        self.username = event_data['performer']['user_text']
        self.user_type = "bot" if event_data['performer']['user_is_bot'] else "user"
        self.language = self.get_language(urlparse(event_data["meta"]["uri"]).netloc)
        self.title = event_data['page_title']
        self.is_revert = event_data['rev_is_revert']
