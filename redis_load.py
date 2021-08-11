# do 'pip install pika' if you don't have it
import requests, time, threading, json, random


base_url = "http://localhost:8080/sessionManager"
get_token_url = base_url+"?epr=testEPR1"
save_token_url = base_url
get_all_tokens_url = base_url+"/getAll"
delete_all_url = base_url+"/deleteAll?epr=testEPR1"
init_epr_url = base_url+"/init?epr=testEPR1"
sessionRequests = []
headers = {}
headers['Content-Type'] = "application/json"
headers['Accept'] = "application/json"

#how many entries in the queue you want to make

def _start(n):
    print("Thread " + str(n) + " Started")
    # Getting tokens
    request = requests.get(get_token_url)
    data = json.loads(request.text)
    sessionRequest = {}
    sessionRequest["session"] = data
    sessionRequest['session']['lastUsed'] = int(time.time())
    sessionRequest["epr"] = "testEPR1"
    print("Thread " + str(n) + " has token: " + data['token'])
    time.sleep(random.randint(3,6))
    requests.post(save_token_url, json.dumps(sessionRequest), headers=headers)
    print("Thread " + str(n) + " Stopped")

# requests.delete(delete_all_url)
r = requests.post(init_epr_url)
# print(json.loads(r.text))

nThreads = 1
threads = []
for n in range(nThreads):
    t = threading.Thread( target=_start, args=( n+1, ) )
    t.start() 
    threads.append( t )
for thread in threads:
    thread.join()
