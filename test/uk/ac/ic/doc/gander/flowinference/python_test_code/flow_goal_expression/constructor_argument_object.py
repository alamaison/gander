class A(object):
	def __init__(self, a):
		print self # not self in A
		print a # a in A

x = 2

print x # blastoff

A(x)