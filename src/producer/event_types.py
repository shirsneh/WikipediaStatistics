import json
from dataclasses import dataclass
from urllib.parse import urlparse

from dataclasses_jsonschema import JsonSchemaMixin
from pprint import pprint

lang_codes = json.loads(open("language-and-locale-codes.json").read())


@dataclass
class Event():
    event_type: str
    event_id: int
    timestamp: str
    username: str
    user_type: str
    language: str
    title: str

    def __init__(self, event_data):
        if self.get_matching_event(event_data):
            self.event_type = "new"
            self.event_id = event_data['rev_id']
            self.timestamp = event_data['meta']['dt']
            self.username = event_data['performer']['user_text']
            self.user_type = "bot" if event_data['performer']['user_is_bot'] else "user"
            self.language = self.get_language(urlparse(event_data["meta"]["uri"]).netloc)
            self.title = event_data['page_title']
            self.is_revert = event_data['rev_is_revert']
        else:
            self.event_type = "edit"
            self.event_id = event_data['id']
            self.timestamp = event_data['meta']['dt']
            self.username = event_data['user']
            self.user_type = "bot" if event_data['bot'] else "user"
            self.language = self.get_language(urlparse(event_data["meta"]["uri"]).netloc)
            self.title = event_data['title']
            self.is_revert = False

    @staticmethod
    def get_matching_event(event_data):
        return True if "create" in event_data["$schema"] else False

    @staticmethod
    def get_language(url: str) -> str:
        for lang in lang_codes:
            if lang["code"] + "." in url or "." + lang["code"] in url:
                return lang["desc"]
        return "English"

    def toJSON(self):
        # return "{\n    " + ",\n    ".join(['{}: "{}"'.format(key, self.__dict__[key]) for key in self.__dict__.keys()]) + "}"
        return json.dumps(self.__dict__, sort_keys=True)
