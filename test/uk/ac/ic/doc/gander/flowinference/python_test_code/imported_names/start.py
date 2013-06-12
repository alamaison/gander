import gertrude
import children.bobby
import stepchildren

# This import should result in stepchildren getting the symbol uglychild added
# to it where it wouldn't otherwise (uglychild isn't imported by stepchildren
# and is never imported as uglychild by anyone)
import stepchildren.uglychild as william

def alice():
	pass

class Bob:
	def charles(self):
		# nested imports appear in the local scope but not outside it
		import mercurial
		from gertrude import Iris