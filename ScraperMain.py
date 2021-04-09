import requests
from bs4 import BeautifulSoup
from mezmorize import Cache

cache = Cache(CACHE_TYPE='filesystem', CACHE_DIR='cache')
    
@cache.memoize()
def fibonacci(n):
    print("fib",n)
    if n < 2:
        return n
    return fibonacci(n-1) + fibonacci(n-2)
    
    
print("fib 100",fibonacci(100))