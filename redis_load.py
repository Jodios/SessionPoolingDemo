# do 'pip install pika' if you don't have it
import requests, time, threading, json, random


base_url = "http://localhost:8080/sessionManager"
get_token_url = base_url+"?epr=testEPR1"
save_token_url = base_url
get_all_tokens_url = base_url+"/getAll"

#how many entries in the queue you want to make
n = 1000

def _start(n):
    print("Thread " + n + " Started")
    sessionRequests = []
    headers = {}
    headers['Content-Type'] = "application/json"
    headers['Accept'] = "application/json"
    # Getting tokens
    for i in range(4): 
        request = requests.get(get_token_url)
        data = json.loads(request.text)
        sessionRequest = {}
        sessionRequest["session"] = data
        sessionRequest['session']['lastUsed'] = int(time.time())
        sessionRequest["epr"] = "testEPR1"
        sessionRequests.append(sessionRequest)
        time.sleep(random.randint(1,2))
    for sr in sessionRequests:
        requests.post(save_token_url, json.dumps(sr), headers=headers)
        time.sleep(random.randint(1,3))


    print("Thread " + n + " Stopped")

thread1 = threading.Thread(target=_start, args=("1",)).start()
thread2 = threading.Thread(target=_start, args=("2",)).start()
thread3 = threading.Thread(target=_start, args=("3",)).start()
thread4 = threading.Thread(target=_start, args=("4",)).start()
thread5 = threading.Thread(target=_start, args=("5",)).start()
thread6 = threading.Thread(target=_start, args=("6",)).start()
thread7 = threading.Thread(target=_start, args=("7",)).start()
thread8 = threading.Thread(target=_start, args=("8",)).start()
thread9 = threading.Thread(target=_start, args=("9",)).start()
thread10 = threading.Thread(target=_start, args=("10",)).start()
thread11 = threading.Thread(target=_start, args=("11",)).start()
thread12 = threading.Thread(target=_start, args=("12",)).start()
thread13 = threading.Thread(target=_start, args=("13",)).start()
thread14 = threading.Thread(target=_start, args=("14",)).start()
thread15 = threading.Thread(target=_start, args=("15",)).start()
thread16 = threading.Thread(target=_start, args=("16",)).start()
thread17 = threading.Thread(target=_start, args=("17",)).start()
thread18 = threading.Thread(target=_start, args=("18",)).start()
thread19 = threading.Thread(target=_start, args=("19",)).start()
thread20 = threading.Thread(target=_start, args=("20",)).start()
thread21 = threading.Thread(target=_start, args=("21",)).start()
thread22 = threading.Thread(target=_start, args=("22",)).start()
thread23 = threading.Thread(target=_start, args=("23",)).start()
thread24 = threading.Thread(target=_start, args=("24",)).start()