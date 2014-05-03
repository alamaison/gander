# A global that is not defined in the global codeblock

def h():
	global Z
	Z = 7
	print Z # whose_am_i_locally

h()
print Z # whose_am_i_globally