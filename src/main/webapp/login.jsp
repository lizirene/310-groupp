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
					<h4 class="center-align">Login</h4>
					<!-- Input -->
					<div class="row">
						<div class="input-field col s12">
							<input id="username" name="username" type="text"
								v-model="username"
								:class="{invalid: error.username}"
								@change="error.username = null"
							/>
							<label for="username">Username</label>
							<span class="helper-text" :data-error="error.username"></span>
						</div>
						<div class="input-field col s12">
							<input id="password" name="password" type="password"
								v-model="password"
								:class="{invalid: error.password}"
								@change="error.password = null"
							/>
							<label for="password">Password</label>
							<span class="helper-text" :data-error="error.password"></span>
						</div>
					</div>

					<!-- Buttons -->
					<div class="flex-around action-button">
						<a class="btn waves-effect waves-light" href="/register.jsp">Create Account
							<i class="material-icons right">send</i>
						</a>
						<button class="btn waves-effect waves-light" type="submit" :disabled="submitting">Login
							<i class="material-icons right">send</i>
						</button>
					</div>
				</form>
			</div><!-- card -->
		</div><!-- row -->
	</div><!-- container -->
	<!-- javascript -->
	<script src="js/login/login.js" type="module"></script>
</body>
</html>