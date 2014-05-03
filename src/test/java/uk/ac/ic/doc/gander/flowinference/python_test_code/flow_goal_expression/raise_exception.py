class E:
	pass
	
x = E()
print x # blastoff

try:
	raise x
except E as e:
	print e
