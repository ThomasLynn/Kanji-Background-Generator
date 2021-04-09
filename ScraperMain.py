import requests
from bs4 import BeautifulSoup
from functools import wraps
import pickle

def cached(func):
    func.cache = {}
    @wraps(func)
    def wrapper(*args):
        try:
            return func.cache[args]
        except KeyError:
            func.cache[args] = result = func(*args)
            return result   
    return wrapper
    
@cached
def fibonacci(n):
    print("fib",n)
    if n < 2:
        return n
    return fibonacci(n-1) + fibonacci(n-2)
    
    
try:
    with open('fibs.pickle', 'rb') as file:
        fibonacci.cache = pickle.load(file)
except:
    print("unable to load cache")
    
print("cache",fibonacci.cache)
print("fib 100",fibonacci(100))

with open('fibs.pickle', 'wb') as file:
    pickle.dump(fibonacci.cache, file, protocol=pickle.HIGHEST_PROTOCOL)