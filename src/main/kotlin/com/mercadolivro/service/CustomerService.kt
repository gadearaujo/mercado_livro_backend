package com.mercadolivro.service

import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.enums.Profile
import com.mercadolivro.exception.BadRequestException
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.CustomerRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val bookService: BookService,
    private val bCrypt: BCryptPasswordEncoder
){

    fun getAll(name : String?): List<CustomerModel> {
        name?.let {
            return customerRepository.findByNameContaining(name)
        }
        return customerRepository.findAll().toList()
    }

    fun create(customer : CustomerModel): CustomerModel {
        val customerCopy = customer.copy(
            roles = setOf(Profile.CUSTOMER),
            password = bCrypt.encode(customer.password)
        )
        return customerRepository.save(customerCopy)
    }

    fun findById(id: Int): CustomerModel {
        return customerRepository.findById(id).orElseThrow {
            NotFoundException(Errors.ML201.message.format(id), Errors.ML201.code)
        }
    }

    fun loginCustomer(email: String, password: String): List<CustomerModel> {

        if(customerRepository.findByEmailAndPassword(email, password).isEmpty()) {
            println(email)
            println(password)
            throw BadRequestException(Errors.ML203.message.format(email), Errors.ML203.code)
        } else {
            return customerRepository.findByEmailAndPassword(email, password).toList()
        }
    }

    fun update(customer: CustomerModel) {
       if (!customerRepository.existsById(customer.id!!)) {
           throw Exception()
       }
        customerRepository.save(customer)
    }

    fun delete(id: Int) {
        val customer = findById(id)
        bookService.deleteByCustomer(customer)

        customer.status = CustomerStatus.INATIVO

        customerRepository.save(customer)
    }

    fun emailAvailable(email: String): Boolean {

        return !customerRepository.existsByEmail(email)

    }

    fun setProfilePicture(id: Int, file: MultipartFile) {
        println("file: $file")

        val customerModel : CustomerModel = customerRepository.findById(id).orElseThrow()
        customerModel.photoUrl = file.bytes
        customerRepository.save(customerModel)
    }

    fun getProfilePicture(id: Int): ByteArray{
        val user: CustomerModel = customerRepository.findById(id).orElseThrow()
        return user.photoUrl
    }
}