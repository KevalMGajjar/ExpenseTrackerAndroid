package com.example.splitwiseclone.rest_api

import com.example.splitwiseclone.rest_api.security.RequiresAuth
import com.example.splitwiseclone.roomdb.expense.Expense
import com.example.splitwiseclone.roomdb.friends.Friend
import com.example.splitwiseclone.roomdb.groups.Member
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path
import java.math.BigDecimal

data class TokenResponse(val accessToken: String, val refreshToken: String)
data class RefreshTokenRequest(val refreshToken: String)

data class UserRegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val defaultCurrencyCode: String,
    val profilePicUrl: String?,
     val phoneNumber: String?
    )

data class UserLoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String, val refreshToken: String, val userId: String,
    val email: String, val hashedPassword: String, val phoneNumber: String, val profilePicture: String,
    val username: String, val defaultCurrencyCode: String
)

data class GoogleLoginRequest(val idToken: String)

data class SplitDto(
    val owedByUserId: String,
    val owedAmount: Double,
    val owedToUserId: String
)

data class AddFriendsRequest(
    val phoneNumberList: List<String>,
    val currentUserId: String,
    val currentUserEmail: String,
    val currentUserProfilePic: String,
    val currentUserPhoneNumber: String,
    val currentUserUsername: String
)

data class AddFriendResponse(
    val friends: List<Friend>,
    val notFoundPhoneNumbers: List<String>? = null
)

data class GetAllFriendsRequest(
    val currentUserId: String
)

data class DeleteFriendRequest(
    val currentUserId: String,
    val friendId: String
)

data class UpdateFriendBalance(
    val splits: List<SplitDto>
)

data class SaveGroupRequest(
    val groupName: String,
    val profilePicture: String,
    val groupCreatedByUserId: String,
    val type: String
)

data class GroupUpdateRequest(
    val groupId: String,
    val groupName: String,
    val profilePicture: String,
)

data class AddMemberRequest(
    val groupId: String,
    val members: List<Member>
)

data class GetAllGroupsRequest(
    val userId: String
)

data class GroupAddResponse(
    val groupId: String
)

data class GetAllGroupsResponse(
    val groupId: String,
    val groupName: String,
    val profilePic: String,
    val createdByUserId: String,
    val type: String,
    val members: List<MemberResponse>?,
    val archived: Boolean
)

data class MemberResponse(
    val userId: String,
    val role: String,
    val username: String,
    val email: String,
    val profilePicture: String
)

data class UpdateUserRequest(
    val id : String,
    val newProfilePicUrl: String?,
    val newUsername: String?,
    val newPassword: String?,
    val oldPassword: String?,
    val newPhoneNumber: String?
)

data class DeleteGroupRequest(
    val groupId: String,
    val currentUserId: String
)

data class DeleteGroupMembers(
    val groupId: String,
    val membersIds: List<String>
)

data class AddExpenseRequest(
    val groupId: String?,
    val createdByUserId: String,
    val totalExpense: Double,
    val description: String,
    val splitType: String,
    val splits: List<SplitDto>,
    val currencyCode: String,
    val paidByUserIds: List<String>,
    val participants: List<String>,
    val expenseDate: String
)

data class ExpenseResponse(
    val id: String,
    val description: String?,
    val totalExpense: Double,
    val splits: List<ResponseSplitDto>,
    val currencyCode: String,
    val paidByUserIds: List<String>,
    val participants: List<String>,
    val groupId: String? = null,
    val expenseDate: String,
    val splitType: String,
    val createdByUserId: String,
    val deleted: Boolean
)

data class ResponseSplitDto(
    val id: String,
    val owedByUserId: String,
    val owedAmount: Double,
    val owedToUserId: String
)

data class GetAllExpenseRequest(
    val currentUserId: String
)

data class UpdateFriendResponse(
    val id: String,
    val friendId: String,
    val profilePic: String,
    val username: String,
    val phoneNumber: String?,
    val currentUserId: String,
    val email: String?,
    val balanceWithUser: Double?

)

data class UpdateExpenseRequest(
    val id: String,
    val description: String,
    val totalExpense: BigDecimal,
    val splitType: String,
    val splits: List<RequestSplitDto>,
    val currencyCode: String,
    val paidByUserIds: List<String>,
    val participants: List<String>,
    val groupId: String?,
)

data class RequestSplitDto(
    val owedByUserId: String,
    val owedAmount: Double,
    val owedToUserId: String
)

interface RestApiService {

    @POST("auth/register")
    suspend fun registerUser(@Body user: UserRegisterRequest): Response<Any>

    @POST("auth/login")
    suspend fun loginUser(@Body user: UserLoginRequest): LoginResponse

    @POST("auth/refresh")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<TokenResponse>

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): LoginResponse

    @POST("/friends/add")
    @RequiresAuth
    suspend fun addFriends(@Body request: AddFriendsRequest): AddFriendResponse

    @POST("/friends/getAll")
    @RequiresAuth
    suspend fun getAllFriends(@Body request: GetAllFriendsRequest): List<Friend>

    @DELETE("friends/delete")
    @RequiresAuth
    suspend fun deleteFriend(@Body request: DeleteFriendRequest)

    @POST("friends/updateBalance")
    @RequiresAuth
    suspend fun updateFriendBalance(@Body request: UpdateFriendBalance)

    @POST("user/update")
    @RequiresAuth
    suspend fun updateCurrentUser(@Body request: UpdateUserRequest)

    @POST("/group/add")
    @RequiresAuth
    suspend fun addGroup(@Body request: SaveGroupRequest): GroupAddResponse

    @DELETE("/group/delete")
    @RequiresAuth
    suspend fun deleteGroup(@Body request: DeleteGroupRequest)

    @POST("/group/update")
    @RequiresAuth
    suspend fun updateGroup(@Body request: GroupUpdateRequest)

    @POST("/group/addMembers")
    @RequiresAuth
    suspend fun addMembers(@Body request: AddMemberRequest)

    @POST("/group/deleteMembers")
    @RequiresAuth
    suspend fun deleteMembers(@Body request: DeleteGroupMembers)

    @POST("/group/getAll")
    @RequiresAuth
    suspend fun getAllGroups(@Body request: GetAllGroupsRequest): List<GetAllGroupsResponse>

    @POST("expense/add")
    @RequiresAuth
    suspend fun addExpense(@Body request: AddExpenseRequest): ExpenseResponse

    @POST("expense/getAll")
    @RequiresAuth
    suspend fun getAllExpenses(@Body request: GetAllExpenseRequest): List<ExpenseResponse>

    @POST("/expense/update")
    suspend fun updateExpense(@Body request: UpdateExpenseRequest): ExpenseResponse

    @DELETE("/expense/delete/{id}")
    suspend fun deleteExpense(@Path("id") expenseId: String): Response<Void>
}