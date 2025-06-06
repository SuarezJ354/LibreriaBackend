import csv
import psycopg2
import sys

table_name = sys.argv[1] if len(sys.argv) > 1 else None
if not table_name:
    print("❌ No se especificó la tabla")
    sys.exit(1)

conn = psycopg2.connect(
    dbname="railway",
    user="postgres",
    password="LMJLgMRSLMzOfkaMyXuPPWmmDCBwRVtA",
    host="interchange.proxy.rlwy.net",
    port="40432"
)

cur = conn.cursor()
cur.execute(f"SELECT * FROM {table_name}")

filename = f"{table_name}.csv"
with open(filename, "w", newline='') as f:
    writer = csv.writer(f)
    writer.writerow([desc[0] for desc in cur.description])
    writer.writerows(cur.fetchall())

cur.close()
conn.close()

print(f"✅ Exportación completada: {filename}")
