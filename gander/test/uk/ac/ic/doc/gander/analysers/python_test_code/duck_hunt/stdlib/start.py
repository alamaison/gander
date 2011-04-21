import urllib2

def main():
	x = urllib2.Request("http://www.google.co.uk")
	x.has_data() # Request
	x.get_data() # Request