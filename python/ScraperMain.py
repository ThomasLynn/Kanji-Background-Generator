import requests
from bs4 import BeautifulSoup
from mezmorize import Cache

cache = Cache(CACHE_TYPE='filesystem', CACHE_DIR='cache')
    
#@cache.memoize()
def get_jisho_data(url):
    page = requests.get(url)
    soup = BeautifulSoup(page.content, 'html.parser')
    results = soup.find(id='kanji-details__main-meanings')
    print("results",results)
    return None
    
    
def scrape_list(jisho_list_filename):
    with open(jisho_list_filename) as f:
        urls = f.readlines()
        urls = [x.strip() for x in urls] 
        urls = [value for value in urls if value != ""]
    
    for w in urls:
        get_jisho_data(w)
    
scrape_list("lists/jisho_small.txt")