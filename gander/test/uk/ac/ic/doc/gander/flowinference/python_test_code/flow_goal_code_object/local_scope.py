def f():
	print "I am f.  Nice to meet you."

def g():
	print "I am g.  Grrrr."

print f # f flows here
print g # g flows here, not f

class Bob:
	print f # f also flows here
