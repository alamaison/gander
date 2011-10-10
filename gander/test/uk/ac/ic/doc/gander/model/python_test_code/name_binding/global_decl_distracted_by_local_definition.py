# A global that is not defined in the global codeblock because it is a builting
# might be incorrectly resolved if distracted by the assignment to the same name
# (non-globally) in another codeblock in the module.

def h():
	global len
	print len # whose_am_i_locally

def g():
	len = 7


h()
g()
print len # whose_am_i_globally