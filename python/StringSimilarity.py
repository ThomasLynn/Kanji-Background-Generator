from difflib import SequenceMatcher

def get_string_similarity(a, b):
    return SequenceMatcher(None, a, b).ratio()
    
def get_similarity(sentence, sim_list):
    similarity = 0.0
    for w in sim_list:
        similarity += get_string_similarity(sentence, w)
    return similarity
    
def get_least_similar(sentence_list, sim_list):
    similarity = []
    for w in sentence_list:
        similarity.append(get_similarity(w,sim_list))
    #print("sim list",similarity)
    return sentence_list[similarity.index(min(similarity))]
    
def sort_similar(data_list_arg, key = lambda x:x):
    
    data_list = data_list_arg[:]
    new_data_list = []
    sen_list = []
    for w in data_list:
        sen_list.append(key(w))
    new_list = []
    while len(sen_list)>0:
        least_sim = get_least_similar(sen_list,new_list)
        new_list.append(least_sim)
        position = sen_list.index(least_sim)
        sen_list.pop(position)
        new_data_list.append(data_list.pop(position))
    return new_data_list