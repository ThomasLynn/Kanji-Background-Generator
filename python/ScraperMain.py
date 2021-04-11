import requests
from bs4 import BeautifulSoup
from mezmorize import Cache
import urllib.parse
import re
from ImageCreator import *
import argparse

parser = argparse.ArgumentParser()
parser.add_argument("-list", default="lists/jisho_single.txt")
parser.add_argument("-config", default="ImageConfig.ini")
args = parser.parse_args()
print("args",args.list)
list_filename = args.list
config_filename = args.config

cache = Cache(CACHE_TYPE='filesystem', CACHE_DIR='cache', CACHE_DEFAULT_TIMEOUT = 1e10)
    
@cache.memoize()
def get_web_data(url):
    page = requests.get(url)
    return str(page.text)
    
def scrape_jisho_data(url):
    print("getting jisho url",url)
    soup = BeautifulSoup(get_web_data(url), 'html.parser')
    data = {}
    
    kanji = soup.find(class_='character')
    kanji = kanji.get_text()
    #print("kanji",kanji)
    data['kanji'] = kanji
    
    meaning = soup.find(class_='kanji-details__main-meanings')
    meaning = meaning.get_text()
    meaning = meaning.strip()
    #print("meaning",meaning)
    data['meaning'] = meaning
    
    sentences = scrape_tatoeba_data(
        "https://tatoeba.org/eng/sentences/search?from=jpn&query="+urllib.parse.quote(kanji)+"&to=eng"
    )
    #print("sentences",sentences)
    data['sentences'] = sentences
    return data
    
def scrape_tatoeba_data(url):
    print("getting tatoeba url",url)
    soup = BeautifulSoup(get_web_data(url), 'html.parser')
    
    sections = soup.find_all("div",class_="sentence-and-translations md-whiteframe-1dp")
    sentences = []
    for w in sections:
        string = re.sub(r'\\u([0-9a-fA-F]{4})',lambda m: chr(int(m.group(1),16)),w["ng-init"])
        japanese = re.search(r',"text":"(.*?)","lang":"jpn"', string).group(1)
        
        # this is a really hacky way of doing this,
        # if anyone actually knows regex, please change this
        english = re.findall(',"gne":"gnal","(.*?)":"txet"', string[::-1])
        for i in range(len(english)):
            english[i] = english[i][::-1]
        english = list(set(english))
        sentences.append((japanese, english))
    return sentences
    
    
def create_images_from_file(jisho_list_filename, image_creator):
    with open(jisho_list_filename) as f:
        urls = f.readlines()
        urls = [x.strip() for x in urls] 
        urls = [value for value in urls if value != ""]
    
    for w in urls:
        scraped_data = scrape_jisho_data(w)
        image_creator.create_image(scraped_data)
    
    
if __name__ == "__main__":
    image_creator = ImageCreator(config_filename)
    create_images_from_file(list_filename, image_creator)