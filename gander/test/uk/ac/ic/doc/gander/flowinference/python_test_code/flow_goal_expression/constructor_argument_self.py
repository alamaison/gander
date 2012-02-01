class A:
	def __init__(self, a):
		print self # self in A
		print a # not a in A

x = 2

print A(x) # blastoff