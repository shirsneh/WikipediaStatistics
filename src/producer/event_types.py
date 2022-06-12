import json
from dataclasses import dataclass
# from dataclasses_json import dataclass_json

from pprint import pprint
from urllib.parse import urlparse

lang_codes = json.load(open('langs.json', encoding="utf8"))

TOPIC_URLs = {'recentchange': "https://stream.wikimedia.org/v2/stream/mediawiki.recentchange",
              'revision': "https://stream.wikimedia.org/v2/stream/mediawiki.page-create"}

# @dataclass_json
@dataclass
class WikiEvent:
    event_type: str
    event_id: int
    timestamp: str
    username: str
    user_type: str
    language: str
    title: str
    is_revert: bool

    def __init__(self, event_data):
        if "page-create" in event_data["$schema"]:
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
            self.title = event_data['title']
            self.user_type = "bot" if event_data['bot'] else "user"
            self.username = event_data['user']
            self.is_revert = False
            self.language = self.get_language(urlparse(event_data["meta"]["uri"]).netloc)
            self.event_id = event_data['id']
            self.timestamp = event_data['meta']['dt']

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
        json_str = json.dumps(self.__dict__, sort_keys=True, indent=4)
        pprint(json_str)
        return json_str
