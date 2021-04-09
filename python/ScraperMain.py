import requests
from bs4 import BeautifulSoup
from mezmorize import Cache

cache = Cache(CACHE_TYPE='filesystem', CACHE_DIR='cache')
    
@cache.memoize()
def get_jisho_data(url):
    page = requests.get(url)
    return str(page.content)
    
def scrape_jisho_data(url):
    print("getting url",url)
    print("types",type(url),type(get_jisho_data(url)))
    soup = BeautifulSoup(get_jisho_data(url), 'html.parser')
    results = soup.find(class_='kanji-details__main-meanings')
    text = results.get_text()
    text = text[2:].strip()[:-2]
    print("results",text)
    
def scrape_list(jisho_list_filename):
    with open(jisho_list_filename) as f:
        urls = f.readlines()
        urls = [x.strip() for x in urls] 
        urls = [value for value in urls if value != ""]
    
    for w in urls:
        scrape_jisho_data(w)
    
scrape_list("lists/jisho_small.txt")