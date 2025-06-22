from flask import Flask, request, jsonify
from .service.messageService import MessageService
from kafka import KafkaProducer
from kafka.errors import NoBrokersAvailable
import json
import os
import socket
import time

print("Fielding set laadle 🔥🔥🔥🔥🔥🔥🔥🔥")

app = Flask(__name__)
app.config.from_pyfile('config.py')

messageService = MessageService()

# --- Kafka Setup ---
kafka_host = os.getenv('KAFKA_HOST', 'localhost')
kafka_port = os.getenv('KAFKA_PORT', '9092')
kafka_bootstrap_servers = f"{kafka_host}:{kafka_port}"
print("Kafka server is " + kafka_bootstrap_servers)

# Try connecting to Kafka socket first for clearer error
def is_kafka_reachable(host, port, timeout=5):
    try:
        with socket.create_connection((host, int(port)), timeout=timeout):
            return True
    except Exception as e:
        print(f"❌ Kafka not reachable at {host}:{port} → {e}")
        return False

def get_kafka_producer(retries=5, delay=10):
    for i in range(retries):
        try:
            producer = KafkaProducer(
                bootstrap_servers=kafka_bootstrap_servers,
                value_serializer=lambda v: json.dumps(v).encode('utf-8')
            )
            print(f"✅ KafkaProducer connected to {kafka_bootstrap_servers}")
            return producer
        except NoBrokersAvailable:
            print(f"❌ Kafka not reachable, retrying in {delay} seconds... ({i+1}/{retries})")
            time.sleep(delay)
    print("❌ Failed to connect to Kafka after multiple retries.")
    return None

producer = get_kafka_producer()

# --- Flask Endpoints ---

@app.route('/v1/ds/message', methods=['POST'])
def handle_message():
    user_id = request.headers.get('x-user-id')
    if not user_id:
        return jsonify({'error': 'x-user-id header is required'}), 400

    message = request.json.get('message')
    result = messageService.process_message(message)

    if result is not None:
        result.user_id = user_id
        serialized_result = result.serialize()

        if producer:
            try:
                producer.send('expense_service', serialized_result)
                print("✅ Message sent to Kafka topic 'expense_service'")
            except Exception as e:
                print(f"❌ Kafka send failed: {e}")
        else:
            print("⚠️ KafkaProducer is not initialized. Skipping send.")

        return jsonify(serialized_result)
    else:
        return jsonify({'error': 'Invalid message format'}), 400

@app.route('/', methods=['GET'])
def handle_get():
    return 'Hello world'

@app.route('/health', methods=['GET'])
def health_check():
    return 'OK'

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8010, debug=True)  # use 0.0.0.0 inside Docker
