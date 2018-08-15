import bluetooth
import RPi.GPIO as GPIO        #calling for header file which helps ini using GPIOs of PI
import time

LED=21 
GPIO.setmode(GPIO.BCM)     #programming the GPIO by BCM pin numbers. (like PIN40 as GPIO21)
GPIO.setwarnings(False)
GPIO.setup(LED,GPIO.IN)  #initialize GPIO21 (LED) as an output Pin
 
server_socket=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
server_socket.bind(("",bluetooth.PORT_ANY))
server_socket.listen(1)
port = server_socket.getsockname()[1]
uuid = 'f2801eef-31c3-4eb7-a95f-e635ada1fab4'
port = 1
client_socket = 0
address = 0
bluetooth.advertise_service(server_socket,"MyServer" ,service_id=uuid)
elapsedtime = 0
current = 0
#startServer()

def onRise(channel):
    global client_socket
    global current
    global elapsedtime
    if client_socket != 0:
        if elapsedtime == 0:
            current = time.time()
            elapsedtime = 1
        else:
            elapsedtime = time.time()-current
            client_socket.send(str(elapsedtime))
            current = time.time()

GPIO.add_event_detect(LED, GPIO.RISING, callback=onRise, bouncetime=30)

def startRecording():
    print("Starting...")

def stopRecording():
    print("Stopping")

def startServer():
    try:
        global client_socket
        client_socket,address = server_socket.accept()
        print ("Accepted connection from ",address)
        while 1:
         data = client_socket.recv(1024)
         print (data)
         if (data == "0"):    #if '0' is sent from the Android App, turn OFF the LED
          stopRecording()
         if (data == "1"):    #if '1' is sent from the Android App, turn OFF the LED
          startRecording()
         if (data == "q"):
          print ("Quit")
          break
    except bluetooth.btcommon.BluetoothError as e:
        print("Connection ended, restarting")
        #client_socket.close()
        #server_socket.close()
        #startServer()

while 1:
    startServer()
 
client_socket.close()
server_socket.close()
