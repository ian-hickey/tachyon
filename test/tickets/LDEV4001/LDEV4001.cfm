<cfparam name="FORM.scene" default="">

<cfif form.scene == 1>
	<cfjava handle="newClass">
		public class NewClass {
			private String value;
			
			public NewClass(String value) {
				this.value = value;
			}

			public String getValue() {
				return value;
			}
		}
	</cfjava>
	<cfset newClass.init("Tachyon")>
	<cfoutput>#newClass.getValue()#</cfoutput>

<cfelseif form.scene == 2>
	<cfscript>
		classInstance = java{
			public class class1{
				public String execute() {
					return "java block worked in Tachyon";
				}
			}
		}
		writeoutput(classInstance.execute()) 
	</cfscript>
</cfif>