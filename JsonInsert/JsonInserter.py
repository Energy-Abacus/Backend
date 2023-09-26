import mysql.connector
import json

host = "localhost"
user = "root"
password = "123"
database = "quarkus"

with open("device_types.json", "r") as json_file:
    device_types_data = json.load(json_file)

def convertToBinaryData(filename):
    with open(filename, 'rb') as file:
        binaryData = file.read()
    return binaryData

try:
    connection = mysql.connector.connect(
        host=host,
        user=user,
        password=password,
        database=database
    )

    if connection.is_connected():
        print("Connected to MySQL database")

        cursor = connection.cursor()
        insert_query = "INSERT INTO DeviceType (expectedConsumption, icon, name) VALUES (%s, %s, %s)"

        cursor.execute("SELECT name FROM DeviceType")
        existing_names = {row[0] for row in cursor.fetchall()}

        for device_type in device_types_data:
            if device_type["name"] not in existing_names:
                values = (
                    device_type["expectedConsumption"],
                    convertToBinaryData(device_type["icon"]),
                    device_type["name"]
                )
                cursor.execute(insert_query, values)
                existing_names.add(device_type["name"])

        connection.commit()

        print("New data inserted successfully")

except mysql.connector.Error as e:
    print(f"Error: {e}")
finally:
    if 'connection' in locals() and connection.is_connected():
        cursor.close()
        connection.close()
        print("MySQL connection closed")