import requests
from bs4 import BeautifulSoup
from mezmorize import Cache
import urllib.parse
import re

cache = Cache(CACHE_TYPE='filesystem', CACHE_DIR='cache', CACHE_DEFAULT_TIMEOUT = 1e10)
    
@cache.memoize()
def get_web_data(url):
    page = requests.get(url)
    return str(page.text)
    
def scrape_jisho_data(url):
    print("getting jisho url",url)
    soup = BeautifulSoup(get_web_data(url), 'html.parser')
    
    kanji = soup.find(class_='character')
    kanji = kanji.get_text()
    print("kanji",kanji)
    
    meaning = soup.find(class_='kanji-details__main-meanings')
    meaning = meaning.get_text()
    meaning = meaning.strip()
    print("meaning",meaning)
    
    sentences = scrape_tatoeba_data(
        "https://tatoeba.org/eng/sentences/search?from=jpn&query="+urllib.parse.quote(kanji)+"&to=eng"
    )
    
def scrape_tatoeba_data(url):
    print("getting tatoeba url",url)
    soup = BeautifulSoup(get_web_data(url), 'html.parser')
    
    sections = soup.find_all("div",class_="sentence-and-translations md-whiteframe-1dp")
    print("found",len(sections),"sections")
    for w in sections:
        string = re.sub(r'\\u([0-9a-fA-F]{4})',lambda m: chr(int(m.group(1),16)),w["ng-init"])
        japanese = re.search(r',"text":"(.*?)","lang":"jpn"', string).group(1)
        print("japanese",japanese)
        
        # this is a really hacky way of doing this,
        # if anyone actually knows regex, please change this
        english = re.findall(',"gne":"gnal","(.*?)":"txet"', string[::-1])
        for i in range(len(english)):
            english[i] = english[i][::-1]
        english = list(set(english))
        print("english", english)
    return sections
    
    
def scrape_list(jisho_list_filename):
    with open(jisho_list_filename) as f:
        urls = f.readlines()
        urls = [x.strip() for x in urls] 
        urls = [value for value in urls if value != ""]
    
    for w in urls:
        scrape_jisho_data(w)
    
scrape_list("lists/jisho_single.txt")