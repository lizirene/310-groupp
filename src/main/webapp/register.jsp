<!DOCTYPE html>
<html lang="en">
<head>
	<%@include file="head.jsp"%>
</head>
<body>
	<%@include file="appbar.jsp"%>

	<!-- Main content -->
	<div class="container" style="margin-top: calc(50vh - 280px);">
	  <div class="row valign-wrapper">
			<div class="card darken-1 col s12 m8 offset-m2">
				<form id="app" class="card-content" action="javascript:;" @submit="onFormSubmit">
					<h4 class="center-align">Create Account</h4>
					<!-- Input -->
					<div class="row">
						<div class="input-field col s12">
							<input name="username" id="username" type="text"
								:class="{invalid: error.username}"
								v-model="username"
								@change="error.username = null"
							/>
							<label for="username">Username</label>
							<span class="helper-text" :data-error="error.username || ''"></span>
						</div>
						<div class="input-field col s12">
							<input name="password" id="password" type="password"
								:class="{invalid: error.password}"
								v-model="password"
								@change="error.password = null"
							/>
							<label for="password">Password</label>
							<span class="helper-text" :data-error="error.password || ''"></span>
						</div>
						<div class="input-field col s12">
							<input name="repeatPassword" id="repeat-password" type="password"
								:class="{invalid: error.repeatPassword}"
								v-model="repeatPassword"
								@change="error.repeatPassword = null"
							/>
							<label for="repeat-password">Repeat Password</label>
							<span class="helper-text" :data-error="error.repeatPassword || ''"></span>
						</div>
					</div>
					
					<!-- Buttons -->
					<div class="flex-around action-button">
						<a class="btn waves-effect waves-light red darken-1" href="/login.jsp">Cancel
							<i class="material-icons right">cancel</i>
						</a>
						<button class="btn waves-effect waves-light" type="submit" :disabled="submitting">Create User
							<i class="material-icons right">send</i>
						</button>
					</div>
				</form>
			</div> <!-- card -->
		</div> <!-- row -->
	</div> <!-- container -->
	<!-- javascript -->
	<script src="js/register/register.js" type="module"></script>
</body>
</html>