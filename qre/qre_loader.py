import argparse
import psycopg2
import urllib

__connect_string = "dbname='%(db)s' user='%(user)s' host='%(server)s' \
        password='%(pwd)s'"
__connect_params = {'server': "babylon.cise.ufl.edu", 'user' : "morpheus3",'pwd' : "crimson03.sql", 'db' : "Morpheus3DB"}

def load_qre(qrmid, code, realmid=82):
	try:
		connection = psycopg2.connect(__connect_string % __connect_params)
	except:
		print ("error connecting to the db")
		return None
	
	code = urllib.quote(code) # Unquote code
	
	q = "UPDATE qrm \
				SET qrmid=%(qrmid)d, code='%(code)s', realmid=%(realmid)d \
				WHERE qrmid=%(qrmid)d;"% {'qrmid':qrmid,'code':code, 'realmid':realmid}
	
	cursor = connection.cursor()
	cursor.execute(q)
	connection.commit()

if __name__ == '__main__':
	parser = argparse.ArgumentParser(description="A loader for the qre",
				add_help=True)
	parser.add_argument("--qrefile", "-qf", required=True)
	parser.add_argument("--qrmid", required=True, type=int)
	parser.add_argument("--realmid", required=True, type=int)
	args = parser.parse_args()

	code = open(args.qrefile,'r').read()
	load_qre(args.qrmid,code,args.realmid)
