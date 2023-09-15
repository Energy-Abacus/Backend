import mysql.connector
import json

# Replace these with your MySQL database credentials
host = "localhost"
user = "root"
password = "123"
database = "quarkus"

# Read data from the JSON file
with open("device_types.json", "r") as json_file:
    device_types_data = json.load(json_file)

def convertToBinaryData(filename):
    # Convert digital data to binary format
    with open(filename, 'rb') as file:
        binaryData = file.read()
    return binaryData

try:
    # Establish a connection to the MySQL server
    connection = mysql.connector.connect(
        host=host,
        user=user,
        password=password,
        database=database
    )

    if connection.is_connected():
        print("Connected to MySQL database")

        # Create a cursor object to interact with the database
        cursor = connection.cursor()

        # Define an INSERT query
        insert_query = "INSERT INTO DeviceType (expectedConsumption, icon, name) VALUES (%s, %s, %s)"

        # Retrieve existing data from the database
        cursor.execute("SELECT name FROM DeviceType")
        existing_names = {row[0] for row in cursor.fetchall()}

        # Insert new data from the JSON file into the database
        for device_type in device_types_data:
            if device_type["name"] not in existing_names:
                values = (
                    device_type["expectedConsumption"],
                    convertToBinaryData(device_type["icon"]),
                    device_type["name"]
                )
                cursor.execute(insert_query, values)
                existing_names.add(device_type["name"])

        # Commit the changes to the database
        connection.commit()

        print("New data inserted successfully")

except mysql.connector.Error as e:
    print(f"Error: {e}")
finally:
    if 'connection' in locals() and connection.is_connected():
        cursor.close()
        connection.close()
        print("MySQL connection closed")